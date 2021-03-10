"use strict";

let staticAssetsBaseDir = process.env.COMPILED_ASSETS_OUTPUT_DIR || "./build/resources/main/static/pattern-library";

module.exports = {
    watchDirs: ["./src"],
    js: [{
        source: "./node_modules/aiur/lib/client/index.js",
        target: `${staticAssetsBaseDir}/script-aiur.js`,
    }],
    sass: [{
        source: "./node_modules/aiur/lib/style.scss",
        target: `${staticAssetsBaseDir}/style-aiur.css`
    }, {
        source: "./src/main/resources/assets/styles/index.scss",
        target: `${staticAssetsBaseDir}/bundle.css`
    }]
};
