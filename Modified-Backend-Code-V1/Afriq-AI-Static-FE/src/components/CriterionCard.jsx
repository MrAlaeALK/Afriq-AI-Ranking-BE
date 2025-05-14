import React from 'react';

const CriterionCard = ({ icon, title, description, bgColor, textColor }) => {
  return (
    <div className="bg-gray-50 p-6 rounded-xl shadow-md border border-gray-100">
      <div className={`w-12 h-12 ${bgColor} rounded-full flex items-center justify-center mb-4`}>
        {icon}
      </div>
      <h4 className="text-xl font-semibold mb-2">{title}</h4>
      <p className="text-gray-600">{description}</p>
    </div>
  );
};
export default CriterionCard;