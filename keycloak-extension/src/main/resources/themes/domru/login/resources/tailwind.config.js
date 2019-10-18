module.exports = {
  theme: {
    screens: {
      xs: '320px',
      sm: '480px',
      md: '640px',
      lg: '960px',
      xl: '1200px',
      xxl: '1440px',
    },
    fontFamily: {
      body: ['Fact', 'Arial', 'sans-serif'],
    },
    extend: {
      fontSize: {
        '4/3em': '1.33em',
        '5/3em': '1.66em',
      },
      flex: {
        'basis-1/2': '0 1 50%',
      },
      spacing: {
        '96': '24rem',
        '111': '111px',
        '128': '32rem',
      },
      height: {
        '30px': '30px',
        '60px': '60px',
      },
      width: {
        '3/7': '42.8%',
        '30px': '30px',
        '60px': '60px',
      },
      minWidth: {
        '1/4': '25%',
        '1/2': '50%',
        '3/4': '75%',
      },
      maxWidth: {
        '1/4': '25%',
        '1/2': '50%',
        '3/4': '75%',
        '440px': '440px',
        '470px': '470px',
      },
      minHeight: {
        '1/4': '25%',
        '1/2': '50%',
        '3/4': '75%',
      },
      maxHeight: {
        '1/4': '25%',
        '1/2': '50%',
        '3/4': '75%',
      },
      borderWidth: {
        '3': '3px',
      },
      borderRadius: {
        full: '50%',
      },
      opacity: {
        '40': 0.4,
        '90': 0.9,
      },
      colors: {
        black: {
          '50': '#00000080',
          '80': '#000000cc',
          default: '#000000',
        },
        main: {
          '100': '#fafafa',
          '200': '#f8f8f8',
          '300': '#f3f3f3',
          '400': '#e2e2e2',
          '500': '#c7c7c7',
          '600': '#a7a7a7',
          '700': '#777777',
          '800': '#222222',
          default: '#777777',
        },
        extra: {
          '100': '#fff6be',
          '300': '#ffe95c',
          '500': '#ffe014',
          '700': '#ffdd00',
          '700-hover': '#ffdf10',
          '900': '#e2c404',
          default: '#ffdd00',
        },
        accentRed: {
          '100': '#ff8a8a',
          '300': '#ff4e53',
          '500': '#f5272d',
          '700': '#e31e24',
          '900': '#b3002b',
          default: '#e31e24',
        },
        accentBlue: {
          '100': '#b0cbff',
          '300': '#8eb0ef',
          '500': '#7296da',
          '700': '#6688c9',
          '700-hover': '#7496D9',
          '700-20': '#6889c933',
          '900': '#1d45a1',
          default: '#6688c9',
        },
        accentGreen: {
          '100': '#8de549',
          '300': '#73d02c',
          '500': '#6cc628',
          '700': '#69be28',
          '900': '#31812e',
          default: '#69be28',
        },
      },
    },
  },
  variants: {},
  plugins: [],
};
