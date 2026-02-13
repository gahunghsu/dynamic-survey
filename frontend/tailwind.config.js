/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#8D6E63', // Example brown from PDF
        secondary: '#D7CCC8', // Example beige
      }
    },
  },
  plugins: [],
}