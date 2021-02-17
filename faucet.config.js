"use strict";

module.exports = {
    watchDirs: ["./src"],
    js: [{
        source: "./node_modules/aiur/lib/client/index.js",
        target: "./build/resources/main/static/script-aiur.js",
    }],
    sass: [{
        source: "./node_modules/aiur/lib/style.scss",
        target: "./build/resources/main/static/style-aiur.css"
    }, {
        source: "./src/main/resources/assets/styles/index.scss",
        target: "./build/resources/main/static/bundle.css"
    }]
};
