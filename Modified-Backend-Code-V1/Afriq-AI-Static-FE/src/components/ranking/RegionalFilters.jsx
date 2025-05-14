// components/ranking/RegionalFilters.js (continuation)
import React from 'react';

const RegionalFilters = ({ activeRegion, setActiveRegion }) => {
  const regions = [
    { id: 'all', name: 'Tous' },
    { id: 'North Africa', name: 'Afrique du Nord' },
    { id: 'West Africa', name: 'Afrique de l\'Ouest' },
    { id: 'East Africa', name: 'Afrique de l\'Est' },
    { id: 'Central Africa', name: 'Afrique Centrale' },
    { id: 'Southern Africa', name: 'Afrique Australe' }
  ];

  return (
    <div className="flex flex-wrap gap-2 w-full md:w-auto">
      {regions.map(region => (
        <button
          key={region.id}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${
            activeRegion === region.id 
              ? 'bg-purple-600 text-white' 
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
          onClick={() => setActiveRegion(region.id)}
        >
          {region.name}
        </button>
      ))}
    </div>
  );
};

export default RegionalFilters;