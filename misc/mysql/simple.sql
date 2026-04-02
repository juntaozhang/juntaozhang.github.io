CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO orders (id, user_id, amount, status)
VALUES
  (1, 101, 99.99, 'CREATED'),
  (2, 102, 49.50, 'CREATED'),
  (3, 101, 10.00, 'PAID');




UPDATE orders SET status = 'PAID', amount = 89.99 WHERE id = 1;
UPDATE orders SET status = 'PAID', amount = 39.99 WHERE id = 2;

DELETE FROM orders WHERE id = 2;



-- INSERT INTO public.orders (id, user_id, amount, status) values (6, 102, 9.99, 'CREATED');
-- DELETE FROM public.orders WHERE id = 6;