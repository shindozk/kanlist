const WEEKDAYS_LONG = ["Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"];
const WEEKDAYS_SHORT = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];
const MONTHS = [
  "janeiro",
  "fevereiro",
  "março",
  "abril",
  "maio",
  "junho",
  "julho",
  "agosto",
  "setembro",
  "outubro",
  "novembro",
  "dezembro",
];
const MONTHS_SHORT = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];

const pad = (n: number) => String(n).padStart(2, "0");

/** Local YYYY-MM-DD */
export function dayKey(d: Date | number) {
  const date = typeof d === "number" ? new Date(d) : d;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function startOfDay(d: Date | number) {
  const date = new Date(d);
  date.setHours(0, 0, 0, 0);
  return date;
}

export function addDays(d: Date, n: number) {
  const date = new Date(d);
  date.setDate(date.getDate() + n);
  return date;
}

/** Monday-first week containing `d`. */
export function weekOf(d: Date) {
  const start = startOfDay(d);
  const offset = (start.getDay() + 6) % 7;
  const monday = addDays(start, -offset);
  return Array.from({ length: 7 }, (_, i) => addDays(monday, i));
}

export const weekdayShort = (d: Date) => WEEKDAYS_SHORT[d.getDay()];

export function longDate(d: Date) {
  return `${WEEKDAYS_LONG[d.getDay()]}, ${d.getDate()} de ${MONTHS[d.getMonth()]} de ${d.getFullYear()}`;
}

export function shortDate(d: Date | number | string) {
  const date = new Date(d);
  if (Number.isNaN(date.getTime())) return "";
  return `${date.getDate()} ${MONTHS_SHORT[date.getMonth()]} ${date.getFullYear()}`;
}

export function greeting(d = new Date()) {
  const h = d.getHours();
  if (h < 5) return "Boa noite";
  if (h < 12) return "Bom dia";
  if (h < 18) return "Boa tarde";
  return "Boa noite";
}

export function currentSeason(d = new Date()): { season: "WINTER" | "SPRING" | "SUMMER" | "FALL"; year: number; label: string } {
  const m = d.getMonth();
  if (m <= 2) return { season: "WINTER", year: d.getFullYear(), label: "Inverno" };
  if (m <= 5) return { season: "SPRING", year: d.getFullYear(), label: "Primavera" };
  if (m <= 8) return { season: "SUMMER", year: d.getFullYear(), label: "Verão" };
  return { season: "FALL", year: d.getFullYear(), label: "Outono" };
}

export function relativeFromNow(ts: number) {
  const diff = ts - Date.now();
  const abs = Math.abs(diff);
  const min = Math.round(abs / 60_000);
  const h = Math.round(abs / 3_600_000);
  const days = Math.round(abs / 86_400_000);
  const txt = min < 60 ? `${min} min` : h < 24 ? `${h} h` : `${days} ${days === 1 ? "dia" : "dias"}`;
  return diff >= 0 ? `em ${txt}` : `há ${txt}`;
}

export function timeHM(ts: number) {
  const d = new Date(ts);
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatMinutes(min: number) {
  if (min < 60) return `${Math.round(min)} min`;
  const h = Math.floor(min / 60);
  const m = Math.round(min % 60);
  if (h >= 48) return `${Math.round(h / 24)} dias`;
  return m ? `${h} h ${m} min` : `${h} h`;
}
