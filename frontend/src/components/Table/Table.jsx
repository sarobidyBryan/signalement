import '../css/Table.css';

const Table = ({
  columns,
  data,
  className = '',
  striped = true,
  bordered = true,
  hoverable = true,
  ...props
}) => {
  const tableClassName = `table ${striped ? 'table-striped' : ''} ${
    bordered ? 'table-bordered' : ''
  } ${hoverable ? 'table-hoverable' : ''} ${className}`;

  return (
    <div className="table-wrapper">
      <table className={tableClassName} {...props}>
        <thead className="table-head">
          <tr>
            {columns.map((column, index) => (
              <th key={index} className="table-header-cell">
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="table-body">
          {data && data.map((row, rowIndex) => (
            <tr key={rowIndex} className="table-row">
              {columns.map((column, colIndex) => (
                <td key={colIndex} className="table-cell">
                  {column.render
                    ? column.render(row[column.key], row, rowIndex)
                    : row[column.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
