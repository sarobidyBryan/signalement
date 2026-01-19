import './Form.css';

const Form = ({
  children,
  onSubmit,
  className = '',
  layout = 'vertical',
  ...props
}) => {
  const formClassName = `form form-${layout} ${className}`;

  return (
    <form className={formClassName} onSubmit={onSubmit} {...props}>
      {children}
    </form>
  );
};

export default Form;
