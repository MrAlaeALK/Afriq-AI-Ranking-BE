import React from 'react';

const CountryTable = ({ countries, selectedCountries, onCountrySelect, requestSort, sortConfig, weights }) => {
  const getClassNamesFor = (name) => {
    if (!sortConfig) {
      return;
    }
    return sortConfig.key === name ? sortConfig.direction : undefined;
  };

  // Map des couleurs pour chaque critère
  const criteriaColors = {
    odin: { bg: 'bg-purple-100', text: 'text-purple-800', bar: 'bg-purple-600' },
    hdi: { bg: 'bg-green-100', text: 'text-green-800', bar: 'bg-green-500' },
    internet: { bg: 'bg-blue-100', text: 'text-blue-800', bar: 'bg-blue-500' },
    education: { bg: 'bg-yellow-100', text: 'text-yellow-800', bar: 'bg-yellow-500' },
    gdp: { bg: 'bg-red-100', text: 'text-red-800', bar: 'bg-red-500' },
    innovation: { bg: 'bg-indigo-100', text: 'text-indigo-800', bar: 'bg-indigo-500' },
    governance: { bg: 'bg-pink-100', text: 'text-pink-800', bar: 'bg-pink-500' },
    health: { bg: 'bg-teal-100', text: 'text-teal-800', bar: 'bg-teal-500' },
    environment: { bg: 'bg-emerald-100', text: 'text-emerald-800', bar: 'bg-emerald-500' }
  };

  // Labels pour les critères
  const criteriaLabels = {
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
  
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full bg-white border border-gray-200">
        <thead>
          <tr className="bg-gray-100">
            <th className="w-12 py-3 px-4 text-left"></th>
            <th 
              className="py-3 px-4 text-left font-medium text-gray-600 cursor-pointer hover:bg-gray-200"
              onClick={() => requestSort('rank')}
            >
              <div className="flex items-center">
                Rang
                <span className="ml-1">
                  {getClassNamesFor('rank') === 'ascending' ? '↑' : getClassNamesFor('rank') === 'descending' ? '↓' : '↕'}
                </span>
              </div>
            </th>
            <th className="w-12 py-3 px-4 text-left"></th>
            <th 
              className="py-3 px-4 text-left font-medium text-gray-600 cursor-pointer hover:bg-gray-200"
              onClick={() => requestSort('name')}
            >
              <div className="flex items-center">
                Pays
                <span className="ml-1">
                  {getClassNamesFor('name') === 'ascending' ? '↑' : getClassNamesFor('name') === 'descending' ? '↓' : '↕'}
                </span>
              </div>
            </th>
            <th 
              className="py-3 px-4 text-left font-medium text-gray-600 cursor-pointer hover:bg-gray-200"
              onClick={() => requestSort('weightedScore')}
            >
              <div className="flex items-center">
                Score global
                <span className="ml-1">
                  {getClassNamesFor('weightedScore') === 'ascending' ? '↑' : getClassNamesFor('weightedScore') === 'descending' ? '↓' : '↕'}
                </span>
              </div>
            </th>
            
            {/* Colonnes dynamiques basées sur les poids sélectionnés */}
            {Object.keys(weights).map(criterion => (
              <th 
                key={criterion}
                className="py-3 px-4 text-left font-medium text-gray-600 cursor-pointer hover:bg-gray-200"
                onClick={() => requestSort(criterion)}
              >
                <div className="flex items-center">
                  <span className={`${criteriaColors[criterion]?.bg || 'bg-gray-100'} ${criteriaColors[criterion]?.text || 'text-gray-800'} px-2 py-1 rounded`}>
                    {criteriaLabels[criterion] || criterion.toUpperCase()}
                  </span>
                  <span className="ml-1">
                    {getClassNamesFor(criterion) === 'ascending' ? '↑' : getClassNamesFor(criterion) === 'descending' ? '↓' : '↕'}
                  </span>
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {countries.map((country) => (
            <tr 
              key={country.id} 
              className={`border-t border-gray-200 ${selectedCountries.includes(country.id) ? 'bg-purple-50' : 'hover:bg-gray-50'}`}
            >
              <td className="py-3 px-4">
                <input 
                  type="checkbox" 
                  className="w-4 h-4 text-purple-600 rounded focus:ring-purple-500"
                  checked={selectedCountries.includes(country.id)}
                  onChange={() => onCountrySelect(country.id)}
                />
              </td>
              <td className="py-3 px-4 font-medium">
                {country.rank}
              </td>
              <td className="py-3 px-4 text-2xl">
                {country.flag}
              </td>
              <td className="py-3 px-4 font-medium">
                {country.name}
              </td>
              <td className="py-3 px-4">
                <div className="flex items-center">
                  <div className="w-full bg-gray-200 rounded-full h-2.5 mr-2">
                    <div 
                      className="bg-purple-600 h-2.5 rounded-full" 
                      style={{ width: `${country.weightedScore || country.scores.global}%` }}
                    ></div>
                  </div>
                  <span className="font-medium">{country.weightedScore || country.scores.global}</span>
                </div>
              </td>
              
              {/* Cellules dynamiques basées sur les poids sélectionnés */}
              {Object.keys(weights).map(criterion => (
                <td key={criterion} className="py-3 px-4">
                  <div className="flex items-center">
                    <div className="w-full bg-gray-200 rounded-full h-2.5 mr-2">
                      <div 
                        className={`${criteriaColors[criterion]?.bar || 'bg-gray-500'} h-2.5 rounded-full`}
                        style={{ width: `${country.scores[criterion] || 0}%` }}
                      ></div>
                    </div>
                    <span>{country.scores[criterion] || 0}</span>
                  </div>
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CountryTable;