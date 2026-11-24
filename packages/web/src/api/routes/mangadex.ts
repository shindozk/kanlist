import { ORPCError } from "@orpc/server";
import { z } from "zod";
import { base } from "../__core/app";

/**
 * Read-only proxy for the MangaDex API. MangaDex blocks browser CORS from
 * non-whitelisted origins, so the web preview of the mobile app routes through here.
 * Native builds call MangaDex directly.
 */
const ALLOWED = /^\/(manga|chapter|cover|statistics\/manga)(\/[A-Za-z0-9-]+)?(\/(aggregate|feed))?$/;

const queryValue = z.union([z.string(), z.number(), z.boolean(), z.array(z.union([z.string(), z.number()]))]);

export const mangadex = {
  get: base
    .input(
      z.object({
        path: z.string().max(200),
        query: z.record(z.string(), queryValue).optional(),
      }),
    )
    .handler(async ({ input }) => {
      if (!ALLOWED.test(input.path)) {
        throw new ORPCError("BAD_REQUEST", { message: "Path not allowed" });
      }
      const params = new URLSearchParams();
      for (const [k, v] of Object.entries(input.query ?? {})) {
        for (const item of Array.isArray(v) ? v : [v]) params.append(k, String(item));
      }
      const qs = params.toString();
      const url = `https://api.mangadex.org${input.path}${qs ? `?${qs}` : ""}`;
      const res = await fetch(url, {
        headers: { Accept: "application/json", "User-Agent": "Kanlist/1.0 (+https://runable.com)" },
        signal: AbortSignal.timeout(15_000),
      });
      if (res.status === 404) throw new ORPCError("NOT_FOUND", { message: "Not found" });
      if (res.status === 429) throw new ORPCError("TOO_MANY_REQUESTS", { message: "Rate limited" });
      if (!res.ok) throw new ORPCError("BAD_GATEWAY", { message: `MangaDex HTTP ${res.status}` });
      return (await res.json()) as unknown;
    }),
};
