import React from 'react';

const PartnerLogo = ({ name, longName }) => {
  return (
    <div className="text-center">
      <div className="w-32 h-32 bg-gray-200 rounded-full mx-auto flex items-center justify-center mb-3">
        <span className="text-gray-500 font-semibold">{name}</span>
      </div>
      <p className="font-medium">{longName}</p>
    </div>
  );
};
export default PartnerLogo;