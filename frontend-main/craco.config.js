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

      return webpackConfig
    },
  },
}
