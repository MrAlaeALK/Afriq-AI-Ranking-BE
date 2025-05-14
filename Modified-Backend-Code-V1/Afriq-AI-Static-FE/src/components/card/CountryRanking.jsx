// components/CountryRanking.js
import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

function CountryRanking({ weights = {}, colorScale = 'green-red' }) {
  const navigate = useNavigate();
  
  const handleRedirect = () => {
    navigate('/classement');
  };
  
  // Données de classement (normalement ces données viendraient d'une API)
  const topCountries = [
    { id: 1, rank: 1, name: 'Rwanda', flag: '🇷🇼', region: 'East Africa',
      scores: { global: 82, odin: 85, hdi: 76, internet: 84, education: 70, gdp: 65, innovation: 68, governance: 78, health: 72, environment: 69 }},
    { id: 2, rank: 2, name: 'Mauritius', flag: '🇲🇺', region: 'East Africa',
      scores: { global: 79, odin: 80, hdi: 88, internet: 70, education: 85, gdp: 82, innovation: 75, governance: 80, health: 84, environment: 77 }},
    { id: 3, rank: 3, name: 'South Africa', flag: '🇿🇦', region: 'Southern Africa',
      scores: { global: 76, odin: 78, hdi: 83, internet: 67, education: 79, gdp: 76, innovation: 82, governance: 72, health: 68, environment: 65 }},
    { id: 4, rank: 4, name: 'Kenya', flag: '🇰🇪', region: 'East Africa',
      scores: { global: 74, odin: 76, hdi: 70, internet: 75, education: 72, gdp: 68, innovation: 77, governance: 73, health: 70, environment: 72 }},
    { id: 5, rank: 5, name: 'Morocco', flag: '🇲🇦', region: 'North Africa',
      scores: { global: 72, odin: 68, hdi: 80, internet: 72, education: 75, gdp: 73, innovation: 70, governance: 68, health: 76, environment: 71 }},
    { id: 6, rank: 6, name: 'Ghana', flag: '🇬🇭', region: 'West Africa',
      scores: { global: 70, odin: 72, hdi: 65, internet: 73, education: 68, gdp: 67, innovation: 69, governance: 75, health: 64, environment: 68 }},
    { id: 7, rank: 7, name: 'Tunisia', flag: '🇹🇳', region: 'North Africa',
      scores: { global: 69, odin: 65, hdi: 79, internet: 68, education: 77, gdp: 71, innovation: 67, governance: 64, health: 75, environment: 70 }},
    { id: 8, rank: 8, name: 'Senegal', flag: '🇸🇳', region: 'West Africa',
      scores: { global: 67, odin: 70, hdi: 60, internet: 69, education: 63, gdp: 59, innovation: 66, governance: 71, health: 62, environment: 67 }},
    { id: 9, rank: 9, name: 'Egypt', flag: '🇪🇬', region: 'North Africa',
      scores: { global: 65, odin: 62, hdi: 73, internet: 64, education: 70, gdp: 68, innovation: 61, governance: 60, health: 72, environment: 59 }},
    { id: 10, rank: 10, name: 'Ivory Coast', flag: '🇨🇮', region: 'West Africa',
      scores: { global: 63, odin: 66, hdi: 55, internet: 67, education: 58, gdp: 62, innovation: 64, governance: 59, health: 57, environment: 63 }},
  ];

  // Function to calculate weighted scores
  const calculateWeightedScore = (countryScores, weights) => {
    if (!weights || Object.keys(weights).length === 0) {
      return countryScores?.global || 0;
    }
    
    let weightedScore = 0;
    let totalWeight = 0;
    
    // Calculate weighted score by multiplying each criteria by its weight
    Object.keys(weights).forEach(criteria => {
      if (countryScores?.[criteria] !== undefined) {
        weightedScore += (countryScores[criteria] * weights[criteria]);
        totalWeight += weights[criteria];
      }
    });
    
    // Normalize the score to 100 if weights were applied
    return totalWeight > 0 ? (weightedScore / totalWeight).toFixed(1) : countryScores?.global || 0;
  };

  // Sort countries by weighted scores
  const sortedCountries = useMemo(() => {
    return [...topCountries].sort((a, b) => {
      const scoreA = calculateWeightedScore(a.scores, weights);
      const scoreB = calculateWeightedScore(b.scores, weights);
      return scoreB - scoreA;
    });
  }, [topCountries, weights]);

  // Apply ranking based on sorted scores
  const rankedCountries = useMemo(() => {
    return sortedCountries.map((country, index) => ({
      ...country,
      rank: index + 1,
      weightedScore: calculateWeightedScore(country.scores, weights)
    }));
  }, [sortedCountries, weights]);

  return (
    <div className="w-full xl:w-1/3">
      <div className="bg-white p-6 rounded-xl shadow-md h-full">
        <h3 className="text-xl font-semibold mb-4">Top 10 pays</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr>
                <th className="pb-3 border-b text-left text-sm font-medium text-gray-500">Rang</th>
                <th className="pb-3 border-b text-left text-sm font-medium text-gray-500">Pays</th>
                <th className="pb-3 border-b text-right text-sm font-medium text-gray-500">Score</th>
              </tr>
            </thead>
            <tbody>
              {rankedCountries.map((country) => (
                <CountryRow key={country.id} country={country} colorScale={colorScale} />
              ))}
            </tbody>
          </table>
        </div>
        
        <div className="mt-6">
          <button className="w-full py-2 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-md transition-colors" onClick={handleRedirect}>
            Voir le classement complet
          </button>
        </div>
      </div>
    </div>
  );
}

function CountryRow({ country, colorScale }) {
  // Get color based on score
  const getColorByScore = (score) => {
    if (score === undefined) return '#cccccc'; // Default gray for unknown scores
    
    if (colorScale === 'green-red') {
      if (score >= 80) return '#109618'; // Dark green
      if (score >= 60) return '#7fbd5a'; // Light green
      if (score >= 40) return '#ffeb3b'; // Yellow
      if (score >= 20) return '#f44336'; // Red
      return '#b71c1c'; // Dark red
    } else if (colorScale === 'blue-red') {
      if (score >= 80) return '#1a237e'; // Dark blue
      if (score >= 60) return '#3f51b5'; // Blue
      if (score >= 40) return '#9c27b0'; // Purple
      if (score >= 20) return '#e91e63'; // Pink
      return '#b71c1c'; // Dark red
    } else if (colorScale === 'purple-yellow') {
      if (score >= 80) return '#4a148c'; // Dark purple
      if (score >= 60) return '#9c27b0'; // Purple
      if (score >= 40) return '#e91e63'; // Pink
      if (score >= 20) return '#ff9800'; // Orange
      return '#ffeb3b'; // Yellow
    }
    
    return '#cccccc'; // Default gray
  };
  
  return (
    <tr className="hover:bg-gray-50">
      <td className="py-3 text-gray-800">{country.rank}</td>
      <td className="py-3 text-gray-800">
        <span className="mr-2">{country.flag}</span>
        {country.name}
      </td>
      <td className="py-3 text-right font-medium" style={{ color: getColorByScore(Number(country.weightedScore)) }}>
        {country.weightedScore}
      </td>
    </tr>
  );
};
export default CountryRanking;