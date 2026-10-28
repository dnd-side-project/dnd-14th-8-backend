import { spawn } from "node:child_process";
import { existsSync, readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";
import { parseEnv } from "node:util";
import { envFile, jdbcEnvironment } from "./neon-env.mjs";

try {
  const values = { ...parseEnv(readFileSync(envFile(), "utf8")), ...process.env };
  const jars = existsSync("build/libs")
    ? readdirSync("build/libs").filter(name => name.endsWith(".jar") && !name.endsWith("-plain.jar"))
    : [];
  if (jars.length !== 1) throw new Error("Build the Spring Boot jar first: npm run build");
  const java = process.env.JAVA_HOME ? join(process.env.JAVA_HOME, "bin/java") : "java";
  const child = spawn(java, ["-Duser.timezone=Asia/Seoul", "-jar", join("build/libs", jars[0])], {
    stdio: "inherit",
    env: { ...values, ...jdbcEnvironment(values), SPRING_PROFILES_ACTIVE: process.env.SPRING_PROFILES_ACTIVE ?? "prod,render" },
  });
  for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => child.kill(signal));
  child.on("error", () => {
    console.error("Could not start Java. Set JAVA_HOME to a Java 25 JDK.");
    process.exitCode = 1;
  });
  child.on("exit", (code, signal) => { process.exitCode = code ?? (signal ? 1 : 0); });
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
}
