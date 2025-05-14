import React, { useState } from 'react';
import FilterPanel from './FilterPanel';
import DisplayOptions from './DisplayOptions';
import MapContainer from './MapContainer';
import CountryRanking from './CountryRanking';
import RegionalTrends from './RegionalTrends';

function MapSection() {
  const [colorScale, setColorScale] = useState('green-red');
  const [weights, setWeights] = useState({
    'odin': 40,
    'hdi': 30,
    'internet': 30
  });
  const [selectedCountry, setSelectedCountry] = useState(null);

  // Gestionnaire pour mettre à jour les poids depuis FilterPanel
  const handleWeightsChange = (newWeights) => {
    setWeights(newWeights);
    // Vous pourriez également recalculer les scores globaux ici si nécessaire
  };

  // Gestionnaire pour mettre à jour le pays sélectionné depuis MapContainer
  const handleCountrySelect = (country) => {
    setSelectedCountry(country);
  };

  return (
    <section className="pb-16">
      <div className="container mx-auto px-4">
        <div className="flex flex-col lg:flex-row gap-6">
          {/* Colonne de gauche avec filtres et options */}
          <div className="w-full lg:w-1/4">
            <FilterPanel onWeightsChange={handleWeightsChange} />
            <DisplayOptions colorScale={colorScale} setColorScale={setColorScale} />
          </div>
          
          {/* Colonne de droite avec carte et tableau */}
          <div className="w-full lg:w-3/4">
            <div className="flex flex-col xl:flex-row gap-6">
              <MapContainer 
                colorScale={colorScale} 
                weights={weights}
                selectedCountry={selectedCountry}
                onCountrySelect={handleCountrySelect}
              />
              <CountryRanking 
                colorScale={colorScale}
                weights={weights} 
                selectedCountry={selectedCountry}
                onCountrySelect={handleCountrySelect}
              />
            </div>
            <RegionalTrends weights={weights} />
          </div>
        </div>
      </div>
    </section>
  );
}

export default MapSection;