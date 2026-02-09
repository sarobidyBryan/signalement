import '../css/List.css';

const List = ({
  items,
  renderItem,
  className = '',
  divider = true,
  ...props
}) => {
  return (
    <ul className={`list ${divider ? 'list-divider' : ''} ${className}`} {...props}>
      {items && items.map((item, index) => (
        <li key={index} className="list-item">
          {renderItem ? renderItem(item, index) : item}
        </li>
      ))}
    </ul>
  );
};

export default List;
