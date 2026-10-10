/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      colors: {
        surface: {
          DEFAULT: '#0c0c0c',
          secondary: '#141414',
          tertiary: '#1a1a1a',
          elevated: '#1e1e1e',
          hover: '#252525',
        },
        border: {
          subtle: '#1f1f1f',
          DEFAULT: '#2a2a2a',
          strong: '#333333',
        },
        accent: {
          amber: '#f59e0b',
          'amber-hover': '#d97706',
          emerald: '#10b981',
          blue: '#3b82f6',
          red: '#ef4444',
          purple: '#a855f7',
          orange: '#f97316',
        },
        // backward compat
        space: '#0c0c0c',
        'space-light': '#141414',
      },
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0', transform: 'translateY(8px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'slide-up': {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'pulse-glow': {
          '0%, 100%': { opacity: '0.4' },
          '50%': { opacity: '0.8' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        'count-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        'fade-in': 'fade-in 0.4s ease-out forwards',
        'slide-up': 'slide-up 0.5s ease-out forwards',
        'pulse-glow': 'pulse-glow 2s ease-in-out infinite',
        shimmer: 'shimmer 1.5s infinite',
        'count-up': 'count-up 0.6s ease-out forwards',
      },
    },
  },
  plugins: [],
}
