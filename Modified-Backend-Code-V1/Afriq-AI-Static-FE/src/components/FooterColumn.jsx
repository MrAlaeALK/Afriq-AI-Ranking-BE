import React from 'react';

const FooterColumn = ({ title, children }) => {
  return (
    <div>
      <h5 className="text-lg font-semibold text-white mb-4">{title}</h5>
      {children}
    </div>
  );
};
export default FooterColumn;