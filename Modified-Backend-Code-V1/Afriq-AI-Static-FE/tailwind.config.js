/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",  // <- ici on inclut tous les fichiers dans src
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}

