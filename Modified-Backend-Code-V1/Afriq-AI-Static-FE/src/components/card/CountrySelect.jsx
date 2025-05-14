import React, { useState, useEffect } from 'react';
import Select from 'react-select';

const CountrySelect = ({ onSelectCountry }) => {
  const [countries, setCountries] = useState([]);

  useEffect(() => {
    fetch('/geojson/africa_map.json')
      .then((response) => response.json())
      .then((data) => {
        const names = data.features.map(
          (feature) =>
            feature.properties?.name ||
            feature.properties?.NAME ||
            feature.properties?.SOVEREIGNT ||
            feature.properties?.merge_group ||
            'Pays inconnu'
        );
        const sorted = [...new Set(names)].sort(); // Supprime les doublons
        const options = sorted.map((name) => ({ value: name, label: name }));
        setCountries(options);
      })
      .catch((err) => {
        console.error('Erreur lors du chargement des pays:', err);
      });
  }, []);

  const handleChange = (selectedOption) => {
    onSelectCountry(selectedOption ? selectedOption.value : '');
  };

  return (
      <div style={{ minWidth: '250px' }}>
        <Select
          options={countries}
          onChange={handleChange}
          isClearable
          placeholder="Sélectionner un pays..."
          styles={{
            control: (base) => ({
              ...base,
              fontSize: '14px',
              borderRadius: '4px',
            }),
          }}
        />
      </div>
  );
};

export default CountrySelect;
