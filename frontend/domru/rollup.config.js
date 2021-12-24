import path from 'path';

import copy from 'rollup-plugin-copy';
import strip from 'rollup-plugin-strip';
import json from 'rollup-plugin-json';
import svelte from 'rollup-plugin-svelte';
import resolve from 'rollup-plugin-node-resolve';
import commonjs from 'rollup-plugin-commonjs';
import { terser } from 'rollup-plugin-terser';
import postcss from 'rollup-plugin-postcss';
import babel from 'rollup-plugin-babel';

const production = !process.env.ROLLUP_WATCH;

const onwarn = warning => {
  // Silence circular dependency warning for svelte package
  if (
    warning.code === 'CIRCULAR_DEPENDENCY' &&
    !warning.importer.indexOf(path.normalize('src/Cities/'))
  )
    return;

  console.warn(`(!) ${warning.message}`);
};

const build = require('./path').build;

export default {
  input: 'src/main.js',
  output: {
    sourcemap: true,
    format: 'iife',
    name: 'app',
    file: `${build}/bundle.min.js`,
  },
  plugins: [
    copy({
      targets: [
        { src: 'src/assets/images/*', dest: `${build}/images` },
        { src: 'src/assets/fonts/*', dest: `${build}/fonts` },
      ],
      copyOnce: !production,
    }),

    json({
      include: ['src/**', 'scripts/**'],

      preferConst: true,
      indent: '  ',
      compact: true,
    }),

    svelte({
      dev: !production,
      css: css => {
        css.write('svelte.css');
      },
    }),

    postcss({
      extensions: ['.css'],
      extract: true,
    }),

    resolve({
      browser: true,
      dedupe: importee =>
        importee === 'svelte' || importee.startsWith('svelte/'),
    }),
    commonjs(),

    production && strip(),

    // compile to good old IE11 compatible ES5
    production &&
    babel({
      extensions: ['.js', '.mjs', '.html', '.svelte'],
      runtimeHelpers: true,
      exclude: ['node_modules/@babel/**', 'node_modules/core-js/**'],
      presets: [
        [
          '@babel/preset-env',
          {
            targets: {
              ie: '11',
            },
            useBuiltIns: 'usage',
            corejs: 3,
          },
        ],
      ],
      plugins: [
        'transform-custom-element-classes',
        '@babel/plugin-syntax-dynamic-import',
        [
          '@babel/plugin-transform-runtime',
          {
            useESModules: true,
          },
        ],
      ],
    }),

    // If we're building for production (npm run build
    // instead of npm run dev), minify
    production && terser(),
  ],
  onwarn,
  watch: {
    clearScreen: false,
  },
};
