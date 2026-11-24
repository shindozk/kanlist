import type { RouterClient } from "@orpc/server";
import { createApp } from "./__core/app";
import { mangadex } from "./routes/mangadex";
import { ping } from "./routes/ping";

// API features are oRPC procedures, one file per feature in ./routes/,
// composed into this router — typed end-to-end via the clients
// (web: src/web/lib/api.ts, mobile: lib/api.ts).
// Keep each routes/ file under 500 lines (`bun run lint` enforces this);
// split into more feature files as they grow.
// Patterns and examples: skills/app/references/api.md
export const router = {
  ping,
  mangadex,
};

export type AppRouter = typeof router;
/** Typed client for the router — used by the web and mobile api clients. */
export type AppRouterClient = RouterClient<AppRouter>;

const app = createApp(router);
// Rare plain-HTTP endpoints (webhooks, streaming, the Better Auth handler)
// register here with full paths, e.g. app.post("/api/webhooks/example", ...)

// Minimal typing for node:https so the mobile typecheck (which includes this file) needs no node types.
interface IncomingLike {
  statusCode?: number;
  headers: Record<string, string | string[] | undefined>;
  on(ev: "data", fn: (d: Uint8Array) => void): void;
  on(ev: "end" | "error", fn: () => void): void;
}
interface HttpsLike {
  get(url: string, opts: { headers: Record<string, string> }, cb: (res: IncomingLike) => void): { on(ev: "error", fn: () => void): void };
}

async function getBinary(url: string): Promise<{ status: number; type: string; body: Uint8Array }> {
  const https = (await import("node:https" as string)) as HttpsLike;
  const fail = { status: 502, type: "", body: new Uint8Array(0) };
  return new Promise((resolve) => {
    https
      .get(url, { headers: { "User-Agent": "Kanlist/1.0" } }, (res) => {
        const chunks: Uint8Array[] = [];
        res.on("data", (d) => chunks.push(d));
        res.on("end", () => {
          const size = chunks.reduce((n, c) => n + c.length, 0);
          const body = new Uint8Array(size);
          let o = 0;
          for (const c of chunks) {
            body.set(c, o);
            o += c.length;
          }
          resolve({ status: res.statusCode ?? 502, type: String(res.headers["content-type"] ?? "image/jpeg"), body });
        });
        res.on("error", () => resolve(fail));
      })
      .on("error", () => resolve(fail));
  });
}

// Streams MangaDex cover images for the web preview (MangaDex blocks hotlinking from browsers).
app.get("/api/img", async (c) => {
  const u = c.req.query("u") ?? "";
  if (!/^https:\/\/uploads\.mangadex\.org\/covers\/[\w-]+\/[\w.-]+$/.test(u)) return c.text("forbidden", 403);
  // node:https instead of fetch: MangaDex's CDN rejects undici's request fingerprint.
  const img = await getBinary(u);
  if (img.status !== 200) return c.text("not found", 404);
  return new Response(img.body as unknown as BodyInit, {
    status: 200,
    headers: { "Content-Type": img.type, "Cache-Control": "public, max-age=604800", "Access-Control-Allow-Origin": "*" },
  });
});

export default app;
