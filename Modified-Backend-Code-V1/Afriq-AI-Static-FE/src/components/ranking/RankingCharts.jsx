import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const RankingCharts = ({ countries, selectedCountries, weights }) => {
  const selectedCountriesData = countries
    .filter(country => selectedCountries.includes(country.id))
    .slice(0, 5); // Maximum 5 pays
  
  // Fonction pour obtenir la couleur d'un critère
  const getCriteriaColor = (criterion) => {
    const colorMap = {
      odin: '#9333ea', // purple-600
      hdi: '#22c55e', // green-500
      internet: '#3b82f6', // blue-500
      education: '#eab308', // yellow-500
      gdp: '#ef4444', // red-500
      innovation: '#6366f1', // indigo-500
      governance: '#ec4899', // pink-500
      health: '#14b8a6', // teal-500
      environment: '#10b981' // emerald-500
    };
    
    return colorMap[criterion] || '#6b7280'; // gray-500 par défaut
  };
  
  // Fonction pour obtenir le label d'un critère
  const getCriteriaLabel = (criterion) => {
    const labelMap = {
      odin: 'ODIN',
      hdi: 'IDH',
      internet: 'Internet',
      education: 'Éducation',
      gdp: 'PIB',
      innovation: 'Innovation',
      governance: 'Gouvernance',
      health: 'Santé',
      environment: 'Environnement'
    };
    
    return labelMap[criterion] || criterion.toUpperCase();
  };
  
  if (selectedCountriesData.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h3 className="text-xl font-bold mb-6">Comparaison graphique</h3>
        <div className="bg-gray-50 p-10 rounded-lg text-center">
          <p className="text-gray-500">
            Sélectionnez des pays dans le tableau pour les comparer
          </p>
        </div>
      </div>
    );
  }
  
  // Préparer les données pour le graphique à barres
  const barData = selectedCountriesData.map(country => {
    const data = { name: country.name };
    
    // Ajouter les scores pour chaque critère sélectionné
    Object.keys(weights).forEach(criterion => {
      data[getCriteriaLabel(criterion)] = country.scores[criterion] || 0;
    });
    
    return data;
  });
  
  // Définir les barres pour chaque critère sélectionné
  const bars = Object.keys(weights).map(criterion => (
    <Bar 
      key={criterion} 
      dataKey={getCriteriaLabel(criterion)} 
      fill={getCriteriaColor(criterion)} 
      name={getCriteriaLabel(criterion)}
    />
  ));
  
  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h3 className="text-xl font-bold mb-6">Comparaison graphique</h3>
      
      <div className="mb-8">
        <h4 className="font-medium text-gray-700 mb-4">Comparaison des scores par critère</h4>
        <ResponsiveContainer width="100%" height={350}>
          <BarChart data={barData}>
            <XAxis dataKey="name" />
            <YAxis domain={[0, 100]} />
            <Tooltip contentStyle={{ backgroundColor: "#f9f9f9", borderRadius: 10 }}
              />
            <Legend />
            {bars}
          </BarChart>
        </ResponsiveContainer>
      </div>
      
      <div className="p-4 bg-gray-50 rounded-lg">
        <h4 className="font-medium text-gray-700 mb-2">Pays sélectionnés</h4>
        <div className="flex flex-wrap gap-2">
          {selectedCountriesData.map(country => (
            <div 
              key={country.id}
              className="flex items-center px-3 py-1 bg-purple-100 text-purple-800 rounded-full"
            >
              <span className="mr-2">{country.flag}</span>
              <span className="font-medium">{country.name}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default RankingCharts;