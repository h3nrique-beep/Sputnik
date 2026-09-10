import { copyFile, mkdir, readdir, access } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import path from "node:path";

// Copia os arquivos do Vite sem apagar bundles que abas abertas ainda possam usar.
const frontend = fileURLToPath(new URL("../", import.meta.url));
const project = path.resolve(frontend, "..");
const dist = path.join(frontend, "dist");
await access(path.join(dist, "index.html"));

async function sync(destination) {
  await mkdir(path.join(destination, "assets"), { recursive: true });
  for (const entry of await readdir(path.join(dist, "assets"), {
    withFileTypes: true,
  })) {
    if (entry.isFile()) {
      await copyFile(
        path.join(dist, "assets", entry.name),
        path.join(destination, "assets", entry.name),
      );
    }
  }
  // O índice só muda quando todos os assets referenciados já estão disponíveis.
  await copyFile(
    path.join(dist, "index.html"),
    path.join(destination, "index.html"),
  );
}

await sync(path.join(project, "src/main/resources/static"));
try {
  await access(path.join(project, "target/classes/static"));
} catch {
  console.log(
    "Frontend sincronizado nos recursos do Spring. Compile o Java para executar.",
  );
  process.exit(0);
}
await sync(path.join(project, "target/classes/static"));
console.log(
  "Frontend sincronizado nos recursos e na saída de compilação do Spring.",
);
