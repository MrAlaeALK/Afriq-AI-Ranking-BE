import { getColor } from './Data';
import { criteriaLabels } from './Data';

export default function CountryCard({ country, index, selectedCriteria }) {
  return (
    <div className="bg-gray-50 rounded-lg shadow overflow-hidden border-t-4" style={{ borderColor: getColor(index) }}>
      <div className="p-4 bg-gray-100">
        <div className="flex items-center justify-between">
          <h3 className="text-xl font-bold">{country.flag} {country.name}</h3>
          <span className="text-sm bg-gray-200 rounded-full px-3 py-1">{country.region}</span>
        </div>
      </div>
      <div className="p-4">
        <div className="space-y-3">
          {Object.entries(country.scores)
            .filter(([key]) => selectedCriteria[key])
            .map(([key, value]) => (
              <div key={key} className="flex items-center">
                <div className="w-1/3 font-medium">
                  {criteriaLabels[key]}
                </div>
                <div className="w-2/3">
                  <div className="w-full bg-gray-200 rounded-full h-3">
                    <div 
                      className="h-3 rounded-full" 
                      style={{ 
                        width: `${value}%`, 
                        backgroundColor: getColor(index)
                      }}
                    ></div>
                  </div>
                  <div className="text-right text-sm mt-1">{value}</div>
                </div>
              </div>
            ))}
        </div>
      </div>
    </div>
  );
}