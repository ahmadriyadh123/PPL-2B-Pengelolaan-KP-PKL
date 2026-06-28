const path = require('path');

module.exports = {
  webpack: {
    configure: (webpackConfig) => {
      // Tambahkan rule untuk menangani file .mjs dari @mui dan @emotion
      // yang menggunakan format ESM (tidak kompatibel langsung dengan webpack 4)
      webpackConfig.module.rules.push({
        test: /\.mjs$/,
        include: /node_modules/,
        type: 'javascript/auto',
      })

      // Resolve ekstensi .mjs agar webpack memprioritaskan CommonJS
      // ketika ada pilihan antara .mjs dan .js
      if (!webpackConfig.resolve.extensions.includes('.mjs')) {
        webpackConfig.resolve.extensions.push('.mjs')
      }

      // Alias antd/dist/antd.css to empty.css for antd v5 compatibility
      webpackConfig.resolve.alias = {
        ...webpackConfig.resolve.alias,
        'antd/dist/antd.css': path.resolve(__dirname, 'src/empty.css'),
      }

      return webpackConfig
    },
  },
}
