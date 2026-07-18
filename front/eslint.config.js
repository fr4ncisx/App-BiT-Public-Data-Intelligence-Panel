// eslint.config.js — Next.js 16 native Flat Config format
import nextConfig from "eslint-config-next";
import nextCoreWebVitalsConfig from "eslint-config-next/core-web-vitals";

const nextFlatConfigs = [
  ...(Array.isArray(nextConfig) ? nextConfig : [nextConfig]),
  ...(Array.isArray(nextCoreWebVitalsConfig)
    ? nextCoreWebVitalsConfig
    : [nextCoreWebVitalsConfig]),
];

// Find the config object from next that registers @typescript-eslint plugin
// so we can extend it rather than redefine it.
const tsEslintConfig = nextFlatConfigs.find(
  (c) => c && typeof c === "object" && "plugins" in c && c.plugins?.["@typescript-eslint"]
);

/** @type {import("eslint").Linter.Config[]} */
const eslintConfig = [
  // Global ignores — standalone object with ONLY the `ignores` key
  {
    ignores: [
      ".next/**",
      "out/**",
      "node_modules/**",
      "public/**",
      "coverage/**",
      "next-env.d.ts",
      "*.config.js",
      "*.config.ts",
    ],
  },

  // Next.js 16 flat config spreads
  ...nextFlatConfigs,

  // TypeScript convention: underscore-prefixed variables are intentionally unused.
  // Must be in the same config object as the @typescript-eslint plugin.
  {
    plugins: tsEslintConfig?.plugins ?? {},
    rules: {
      "@typescript-eslint/no-unused-vars": [
        "warn",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
        },
      ],
    },
  },
];

export default eslintConfig;
