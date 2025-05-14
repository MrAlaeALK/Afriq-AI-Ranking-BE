import React from 'react';

const TestimonialCard = ({ avatar, name, title, quote, bgColor }) => {
  return (
    <div className="bg-gray-50 p-6 rounded-xl shadow-sm border border-gray-100">
      <div className="flex items-center mb-4">
        <div className={`w-12 h-12 ${bgColor} rounded-full mr-4`}></div>
        <div>
          <h5 className="font-semibold">{name}</h5>
          <p className="text-sm text-gray-500">{title}</p>
        </div>
      </div>
      <p className="text-gray-600 italic">{quote}</p>
    </div>
  );
};
export default TestimonialCard;