import { createRequire, register } from "node:module";

//#region rolldown:runtime
var __require = /* @__PURE__ */ createRequire(import.meta.url);

//#endregion
//#region src/loaders/block-network.ts
for (const mod of [
	"net",
	"dns",
	"http",
	"https",
	"tls",
	"dgram"
]) try {
	const m = __require(mod);
	for (const key of Object.keys(m)) m[key] = new Proxy(m[key], { apply() {
		throw new Error("Networking is disabled");
	} });
} catch {}

//#endregion
//#region src/loaders/esm.ts
register(new URL("./hooks.mjs", import.meta.url), import.meta.url);

//#endregion
export {  };