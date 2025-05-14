import CountryCard from './CountryCard';

export default function CountryCardList({ countryData, selectedCriteria }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-md">
      <h2 className="text-xl font-semibold mb-4">Détails des pays sélectionnés</h2>
      <div className="grid grid-cols-1 gap-6">
        {countryData.map((country, index) => (
          <CountryCard 
            key={country.id} 
            country={country} 
            index={index} 
            selectedCriteria={selectedCriteria} 
          />
        ))}
      </div>
    </div>
  );
}