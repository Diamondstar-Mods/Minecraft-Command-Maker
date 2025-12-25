import { Builder } from '.';
/**
 * List of backend frameworks supported by the experimental backends feature
 */
export declare const BACKEND_FRAMEWORKS: readonly ["express", "hono", "h3", "nestjs", "fastify", "elysia"];
export declare const BACKEND_BUILDERS: readonly ["@vercel/express", "@vercel/hono", "@vercel/h3", "@vercel/nestjs", "@vercel/fastify", "@vercel/elysia"];
export type BackendFramework = (typeof BACKEND_FRAMEWORKS)[number];
/**
 * Checks if the given framework is a backend framework
 */
export declare function isBackendFramework(framework: string | null | undefined): framework is BackendFramework;
export declare function isExperimentalBackendsWithoutIntrospectionEnabled(): boolean;
export declare function isExperimentalBackendsEnabled(): boolean;
export declare function isBackendBuilder(builder: Builder | null | undefined): boolean;
/**
 * Checks if experimental backends are enabled AND the framework is a backend framework
 */
export declare function shouldUseExperimentalBackends(framework: string | null | undefined): boolean;
