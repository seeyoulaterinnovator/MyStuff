const convertColorToVar = ({ addBase, theme }) => {
  function extractColorVars (colorObj, colorGroup = '') {
    return Object.entries(colorObj).reduce((vars, [key, value]) => {
      const varKey = key === 'DEFAULT' ? `${colorGroup}` : `${colorGroup}-${key}`
      if (typeof value === 'string') {
        return { ...vars, [`-${varKey}`]: value }
      } else {
        return { ...vars, ...extractColorVars(value, varKey) }
      }
    }, {});
  }

  addBase({
    ':root': extractColorVars(theme('colors')),
  });
}

const gradientsStyles = ({ addUtilities, theme }) => {
  addUtilities({
    '.main-gradient': {
      background:
        'linear-gradient(180deg, var(--first-color-button) 0%, var(--second-color-button) 100%)',
      transition:
        '--first-color-button .2s ease-in-out, --second-color-button .2s ease-in-out, color .2s ease-in-out, box-shadow .2s ease-in-out',
    },
    '.set-gradient-props_white': {
      '--first-color-button': 'var(--white-default)',
      '--second-color-button': 'var(--white-default)',
    },
    '.set-gradient-props_red': {
      '--first-color-button': 'var(--accent-red-100)',
      '--second-color-button': 'var(--accent-red-100)',
    },
    '.set-gradient-props_red-gradient': {
      '--first-color-button': 'var(--accent-red-1200)',
      '--second-color-button': 'var(--accent-red-100)',
    },
  });
};

module.exports = {
  theme: {
    screens: {
      xs: '768px',
      sm: '768px',
      md: '768px',
      lg: '1280px',
      xl: '1280px',
      xxl: '1280px',
    },
    fontFamily: {
      body: ['IBM Plex Sans', 'Arial', 'sans-serif'],
    },
    letterSpacing: {
      tighter: '-.05em',
      tight: '-.025em',
      normal: '0',
      wide: '.025em',
      'wider/sm': '.03em',
      wider: '.05em',
      widest: '.1em',
    },
    extend: {
      fontSize: {
        '4/3em': '1.33em',
        '5/3em': '1.66em',
        'custom/6sm': [
          '.625rem',
          {
            lineHeight: '.875rem',
          },
        ],
        'custom/5sm': [
          '.6875rem',
          {
            lineHeight: '.9375rem',
          },
        ],
        'custom/4sm2': [
          '.75rem',
          {
            lineHeight: '1.0625rem',
          },
        ],
        'custom/4sm': [
          '.75rem',
          {
            lineHeight: '1rem',
          },
        ],
        'custom/3sm': [
          '.8125rem',
          {
            lineHeight: '1.125rem',
          },
        ],
        'custom/2sm': [
          '.875rem',
          {
            lineHeight: '1.1875rem',
          },
        ],
        'custom/sm2': [
          '.9375rem',
          {
            lineHeight: '1.0625rem',
          },
        ],
        'custom/sm': [
          '.9375rem',
          {
            lineHeight: '1.3125rem',
          },
        ],
        'custom/md2': [
          '1rem',
          {
            lineHeight: '1.1875rem',
          },
        ],
        'custom/md': [
          '1rem',
          {
            lineHeight: '1.4375rem',
          },
        ],
        'custom/base': [
          '1.0625rem',
          {
            lineHeight: '1.5rem',
          },
        ],
        'custom/lg': [
          '1.125rem',
          {
            lineHeight: '1.5rem',
          },
        ],
        'custom/2lg': [
          '1.1875rem',
          {
            lineHeight: '1.625rem',
          },
        ],
        'custom/3lg': [
          '1.3125rem',
          {
            lineHeight: '1.75rem',
          },
        ],
        'custom/4lg': [
          '1.4375rem',
          {
            lineHeight: '1.6875rem',
          },
        ],
        'custom/5lg': [
          '1.5rem',
          {
            lineHeight: '1.875rem',
          },
        ],
        'custom/6lg': [
          '1.6875rem',
          {
            lineHeight: '2.0625rem',
          },
        ],
        'custom/7lg': [
          '1.9375rem',
          {
            lineHeight: '2.125rem',
          },
        ],
        'custom/8lg': [
          '2.1875rem',
          {
            lineHeight: '2.5rem',
          },
        ],
        'custom/9lg': [
          '2.875rem',
          {
            lineHeight: '3.25rem',
          },
        ],
      },
      flex: {
        'basis-1/2': '0 1 50%',
        'basis-auto': '0 1 auto',
      },
      spacing: {
        '7': '1.75rem',
        '9': '2.75rem',
        '14': '56px',
        '17': '73px',
        '96': '24rem',
        '128': '32rem',
        '37px': '37px',
        '55px': '55px',
        '111px': '111px',
      },
      height: {
        '14': '56px',
        '30px': '30px',
        '60px': '60px',
      },
      width: {
        '14': '56px',
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
        '200px': '200px',
        '440px': '440px',
        '470px': '470px',
        '696px': '696px',
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
        white: {
          default: '#FFFFFF',
        },
        main: {
          '100': '#fafafa',
          '200': '#f8f8f8',
          '300': '#f3f3f3',
          '400': '#e2e2e2',
          '500': '#c7c7c7',
          '600': '#a7a7a7',
          '700': '#777777',
          '800': '#001E35',
          '900': '#9CA7B4',
          '1000': '#7F7F7F',
          '1100': '#A8A8A8',
          '1200': '#CFDBE0',
          '1300': '#EFEFEF',
          default: '#777777',
        },
        extra: {
          '100': '#ff8a8a',
          '300': '#ff4e53',
          '500': '#f5272d',
          '700': '#C51F1F',
          '700-hover': '#FE403C',
          '900': '#b3002b',
          default: '#C51F1F',
        },
        accentRed: {
          '100': '#FE403C',
          '1200': '#FE403C',
          '300': '#ff4e53',
          '500': '#f5272d',
          '700': '#C51F1F',
          '900': '#b3002b',
          '1000': '#FE403C',
          '1100': '#FE403C',
          '1200': '#FC4C75',
          default: '#C51F1F',
        },
        accentBlue: {
          '100': '#b0cbff',
          '300': '#8eb0ef',
          '500': '#7296da',
          '700': '#6688c9',
          '700-hover': '#7496D9',
          '700-20': '#6889c933',
          '900': '#1d45a1',
          '1000': '#0357F1',
          '1100': '#2B539B',
          '1200': '#1D58BF',
          '1200-hover': '#15418C',
          '1300': '#EFF8FC',
          default: '#6688c9',
        },
        accentGreen: {
          '100': '#8de549',
          '300': '#73d02c',
          '500': '#6cc628',
          '700': '#69be28',
          '900': '#31812e',
          '1000': '#9BC722',
          default: '#69be28',
        },
      },
      boxShadow: {
        base:
          '0px 2px 8px 0px rgba(117, 142, 161, 0.25), 0px 0px 2px 0px rgba(117, 142, 161, 0.22)',
        'base-md': '0px 4px 10px 0px rgba(254, 64, 60, 0.8)',
        main: '0px 0px 2px #758ea138, 0px 4px 12px #758ea138',
        'main-hover': '0px 16px 64px #758ea142',
        'red-hover': '0px 4px 10px #fe403ccc',
        secondary: '0px 15px 30px #758ea133',
        'main-md':
          '0px 4px 8px 0px rgba(117, 142, 161, 0.22), 0px 16px 40px 0px rgba(117, 142, 161, 0.26)',
      },
    },
  },
  variants: {},
  plugins: [convertColorToVar, gradientsStyles],
};
