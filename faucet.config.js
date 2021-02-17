"use strict";

module.exports = {
    js: [{
        source: "./node_modules/aiur/lib/client/index.js",
        target: "./build/resources/main/static/script-aiur.js",
    }],
    sass: [{
        source: "./node_modules/aiur/lib/style.scss",
        target: "./build/resources/main/static/style-aiur.css"
    }]
};
