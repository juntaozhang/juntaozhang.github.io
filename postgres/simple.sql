CREATE TABLE public.orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount float8 NOT NULL, -- todo need to fix DECIMAL(10,2)
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO public.orders (id, user_id, amount, status)
VALUES
  (1, 101, 99.99, 'CREATED'),
  (2, 102, 49.50, 'CREATED'),
  (3, 101, 10.00, 'PAID');




UPDATE public.orders SET status = 'PAID', amount = 89.99 WHERE id = 1;
UPDATE public.orders SET status = 'PAID', amount = 39.99 WHERE id = 2;

DELETE FROM public.orders WHERE id = 2;



-- INSERT INTO public.orders (id, user_id, amount, status) values (6, 102, 9.99, 'CREATED');
-- DELETE FROM public.orders WHERE id = 6;