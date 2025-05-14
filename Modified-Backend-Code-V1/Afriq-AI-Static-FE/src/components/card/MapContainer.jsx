import React, { useState } from 'react';
import MapComponent from './MapComponent';
import CountrySelect from './CountrySelect';
import ColorLegend from './ColorLegend';


function MapContainer({ colorScale, weights, selectedCountry, onCountrySelect }) {
  // Gestion locale du pays sélectionné via le composant CountrySelect
  const handleCountrySelection = (country) => {
    // Transmet la sélection au composant parent
    if (onCountrySelect) {
      onCountrySelect(country);
    }
  };

  return (
    <div className="w-full xl:w-2/3">
      <div className="bg-white p-6 rounded-xl shadow-md relative">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold">Carte de l'Afrique</h3>
          <CountrySelect 
            onSelectCountry={handleCountrySelection} 
            selectedCountry={selectedCountry}
          />
        </div>
        
        {/* Composant de carte */}
        <div className="map-container relative" style={{ height: '400px', width: '100%', marginBottom: '20px' }}>
          <MapComponent 
            selectedCountry={selectedCountry}
            colorScale={colorScale}
            weights={weights}
          />
        </div>

        <ColorLegend colorScale={colorScale} />

      </div>
    </div>
  );
}

export default MapContainer;