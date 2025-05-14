// Données des pays africains
export const data = [
    { id: 1, name: 'Rwanda', flag: '🇷🇼', region: 'East Africa',
   scores: { global: 82, odin: 85, hdi: 76, internet: 84, education: 70, gdp: 65, innovation: 68, governance: 78, health: 72, environment: 69 }},
    { id: 2, name: 'Mauritius', flag: '🇲🇺', region: 'East Africa',
   scores: { global: 79, odin: 80, hdi: 88, internet: 70, education: 85, gdp: 82, innovation: 75, governance: 80, health: 84, environment: 77 }},
    { id: 3, name: 'South Africa', flag: '🇿🇦', region: 'Southern Africa',
   scores: { global: 76, odin: 78, hdi: 83, internet: 67, education: 79, gdp: 76, innovation: 82, governance: 72, health: 68, environment: 65 }},
    { id: 4, name: 'Kenya', flag: '🇰🇪', region: 'East Africa',
   scores: { global: 74, odin: 76, hdi: 70, internet: 75, education: 72, gdp: 68, innovation: 77, governance: 73, health: 70, environment: 72 }},
    { id: 5, name: 'Morocco', flag: '🇲🇦', region: 'North Africa',
   scores: { global: 72, odin: 68, hdi: 80, internet: 72, education: 75, gdp: 73, innovation: 70, governance: 68, health: 76, environment: 71 }},
    { id: 6, name: 'Ghana', flag: '🇬🇭', region: 'West Africa',
   scores: { global: 70, odin: 72, hdi: 65, internet: 73, education: 68, gdp: 67, innovation: 69, governance: 75, health: 64, environment: 68 }},
    { id: 7, name: 'Tunisia', flag: '🇹🇳', region: 'North Africa',
   scores: { global: 69, odin: 65, hdi: 79, internet: 68, education: 77, gdp: 71, innovation: 67, governance: 64, health: 75, environment: 70 }},
    { id: 8, name: 'Senegal', flag: '🇸🇳', region: 'West Africa',
   scores: { global: 67, odin: 70, hdi: 60, internet: 69, education: 63, gdp: 59, innovation: 66, governance: 71, health: 62, environment: 67 }},
    { id: 9, name: 'Egypt', flag: '🇪🇬', region: 'North Africa',
   scores: { global: 65, odin: 62, hdi: 73, internet: 64, education: 70, gdp: 68, innovation: 61, governance: 60, health: 72, environment: 59 }},
    { id: 10, name: 'Ivory Coast', flag: '🇨🇮', region: 'West Africa',
   scores: { global: 63, odin: 66, hdi: 55, internet: 67, education: 58, gdp: 62, innovation: 64, governance: 59, health: 57, environment: 63 }},
    { id: 11, name: 'Nigeria', flag: '🇳🇬', region: 'West Africa',
   scores: { global: 61, odin: 63, hdi: 59, internet: 60, education: 61, gdp: 65, innovation: 62, governance: 57, health: 58, environment: 56 }},
    { id: 12, name: 'Tanzania', flag: '🇹🇿', region: 'East Africa',
   scores: { global: 58, odin: 60, hdi: 54, internet: 59, education: 55, gdp: 53, innovation: 57, governance: 61, health: 56, environment: 62 }},
    { id: 13, name: 'Ethiopia', flag: '🇪🇹', region: 'East Africa',
   scores: { global: 55, odin: 57, hdi: 49, internet: 58, education: 51, gdp: 48, innovation: 54, governance: 56, health: 50, environment: 59 }},
    { id: 14, name: 'Angola', flag: '🇦🇴', region: 'Southern Africa',
   scores: { global: 52, odin: 53, hdi: 58, internet: 46, education: 50, gdp: 60, innovation: 47, governance: 45, health: 54, environment: 51 }},
    { id: 15, name: 'Mozambique', flag: '🇲🇿', region: 'Southern Africa',
   scores: { global: 49, odin: 50, hdi: 45, internet: 51, education: 46, gdp: 44, innovation: 48, governance: 52, health: 47, environment: 53 }},
   ];
   
   // Traduction des critères
   export const criteriaLabels = {
     //global: 'Score Global', 
     odin: 'ODIN',
     hdi: 'IDH',
     internet: 'Accès Internet',
     education: 'Éducation',
     gdp: 'PIB',
     innovation: 'Innovation',
     governance: 'Gouvernance',
     health: 'Santé',
     environment: 'Environnement'
   };
   
   // Fonction pour obtenir une couleur en fonction de l'index
   export const getColor = (index) => {
     const colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6'];
     return colors[index % colors.length];
   };
   
   // Regrouper les pays par région
   export const getCountryGroups = () => {
     const countryGroups = {};
     data.forEach(country => {
       if (!countryGroups[country.region]) {
         countryGroups[country.region] = [];
       }
       countryGroups[country.region].push(country);
     });
     return countryGroups;
   };