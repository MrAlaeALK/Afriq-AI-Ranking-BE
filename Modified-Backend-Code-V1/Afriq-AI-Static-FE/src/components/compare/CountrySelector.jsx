import { getCountryGroups } from './Data';

export default function CountrySelector({ selectedCountries, onCountryChange }) {
  const countryGroups = getCountryGroups();
  
  return (
    <div className="bg-white p-6 rounded-lg shadow-md mb-8">
      <h2 className="text-xl font-semibold mb-4">Sélection des pays</h2>
      <p className="text-sm text-gray-600 mb-4">Sélectionnez au plus 5 pays pour la comparaison</p>
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        {[0, 1, 2, 3, 4].map((index) => (
          <div key={index} className="mb-4">
            <select
              className="w-full p-3 pl-4 pr-10 border border-gray-300 rounded-lg bg-white text-gray-700 text-base focus:outline-none focus:ring-2 focus:ring-purple-600 focus:border-purple-600 appearance-none transition-all"
              value={selectedCountries[index] || ''}
              onChange={(e) => onCountryChange(index, e.target.value)}
            >
              <option value="">Sélectionner un pays</option>
              {Object.keys(countryGroups).map(region => (
                <optgroup key={region} label={region}>
                  {countryGroups[region].map(country => (
                    <option key={country.id} value={country.id}>
                      {country.flag} {country.name}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>
          </div>
        ))}
      </div>
    </div>
  );
}