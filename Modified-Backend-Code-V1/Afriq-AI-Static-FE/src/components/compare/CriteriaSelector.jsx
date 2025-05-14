import { criteriaLabels } from './Data';

export default function CriteriaSelector({ selectedCriteria, criteriaCount, onCriteriaChange }) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-md mb-8">
      <h2 className="text-xl font-semibold mb-4">Sélection des critères</h2>
      <p className="text-sm text-gray-600 mb-4">Sélectionnez au moins 3 critères pour la comparaison</p>
      
      <div className="flex flex-wrap gap-y-2 gap-x-6 mb-4">
        {Object.keys(criteriaLabels).map(criterion => (
          <button
            key={criterion}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${
              selectedCriteria[criterion] 
                ? 'bg-purple-600 text-white' 
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
            onClick={() => onCriteriaChange(criterion)}
          >
            {criteriaLabels[criterion]}
          </button>
        ))}
      </div>
      
      <div className="p-2 bg-blue-50 rounded-md mb-4">
        <p className="text-sm text-blue-800">
          <span className="font-bold">{criteriaCount}</span> critères sélectionnés
          {criteriaCount < 3 && (
            <span className="text-red-500 ml-2">
              (Minimum 3 requis)
            </span>
          )}
        </p>
      </div>
    </div>
  );
}
