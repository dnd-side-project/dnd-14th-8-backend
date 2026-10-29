import { chmodSync, existsSync, readFileSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";
import { parseEnv } from "node:util";
import { pathToFileURL } from "node:url";

export function jdbcEnvironment(values) {
  const raw = values.DATABASE_URL_UNPOOLED;
  if (!raw) throw new Error("DATABASE_URL_UNPOOLED is missing. Run neon deploy first.");
  let url;
  try {
    url = new URL(raw);
  } catch {
    throw new Error("DATABASE_URL_UNPOOLED must be a valid PostgreSQL connection URL.");
  }
  if (!["postgres:", "postgresql:"].includes(url.protocol) || !url.hostname ||
      url.hostname.includes("-pooler.") || !url.username || !url.password || url.pathname.length < 2) {
    throw new Error("A direct PostgreSQL URL with database, username and password is required.");
  }
  let username;
  let password;
  try {
    username = decodeURIComponent(url.username);
    password = decodeURIComponent(url.password);
  } catch {
    throw new Error("PostgreSQL credentials must use valid URL encoding.");
  }
  // Spring Boot is a long-running JDBC app with a small Hikari pool and session timezone.
  // Flyway also needs a direct connection, so neither uses the transaction pooler.
  const params = new URLSearchParams({ sslmode: "require", channelBinding: "require", connectTimeout: "10" });
  return {
    DB_JDBC_URL: `jdbc:postgresql://${url.host}${url.pathname}?${params}`,
    DB_USERNAME: username,
    DB_PASSWORD: password,
  };
}

function dotenvValue(value) {
  if (!/[\s#'"\\]/.test(value)) return value;
  if (/[\r\n]/.test(value)) throw new Error("Database credentials cannot contain line breaks.");
  if (!value.includes("'")) return `'${value}'`;
  if (!value.includes('"')) return `"${value}"`;
  throw new Error("Database credentials cannot safely be written to dotenv with both quote characters.");
}

export function mergeJdbcEnvironment(text) {
  const derived = jdbcEnvironment(parseEnv(text));
  const lines = text.split(/\r?\n/).filter(line => !/^\s*(?:export\s+)?(?:DB_JDBC_URL|DB_USERNAME|DB_PASSWORD)\s*=/.test(line));
  while (lines.at(-1) === "") lines.pop();
  return `${lines.join("\n")}\n\n${Object.entries(derived).map(([key, value]) => `${key}=${dotenvValue(value)}`).join("\n")}\n`;
}

export function envFile() {
  if (existsSync(".env")) return ".env";
  if (existsSync(".env.local")) return ".env.local";
  throw new Error("No .env or .env.local found. Run neon deploy first.");
}

if (process.argv[1] && pathToFileURL(resolve(process.argv[1])).href === import.meta.url) {
  try {
    const path = envFile();
    writeFileSync(path, mergeJdbcEnvironment(readFileSync(path, "utf8")), { mode: 0o600 });
    chmodSync(path, 0o600);
    console.log(`Neon JDBC settings synchronized into ${path}: DB_JDBC_URL, DB_USERNAME, DB_PASSWORD (values hidden)`);
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  }
}
