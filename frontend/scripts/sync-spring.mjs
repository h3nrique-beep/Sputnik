import { copyFile, mkdir, readdir, access, rm } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import path from "node:path";

// Mantém somente o bundle atual: assets antigos também ficam acessíveis por URL direta.
const frontend = fileURLToPath(new URL("../", import.meta.url));
const project = path.resolve(frontend, "..");
const dist = path.join(frontend, "dist");
await access(path.join(dist, "index.html"));

async function sync(destination) {
  const origemAssets = path.join(dist, "assets");
  const destinoAssets = path.join(destination, "assets");
  await mkdir(destinoAssets, { recursive: true });
  const assetsAtuais = await readdir(origemAssets, {
    withFileTypes: true,
  });
  const nomesAtuais = new Set(assetsAtuais.filter((entry) => entry.isFile()).map((entry) => entry.name));
  for (const entry of await readdir(destinoAssets, { withFileTypes: true })) {
    if (!nomesAtuais.has(entry.name)) {
      await rm(path.join(destinoAssets, entry.name), { recursive: true, force: true });
    }
  }
  for (const entry of assetsAtuais) {
    if (entry.isFile()) {
      await copyFile(
        path.join(origemAssets, entry.name),
        path.join(destinoAssets, entry.name),
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
