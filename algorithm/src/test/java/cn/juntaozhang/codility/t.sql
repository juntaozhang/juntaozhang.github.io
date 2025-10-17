SELECT recipient
FROM
  (SELECT
     recipient,
     amount
   FROM
     (
       SELECT
         *,
         ROW_NUMBER()
         OVER (
           PARTITION BY
             recipient
           ORDER BY amount DESC ) AS Row_ID
       FROM transfers
     ) AS A
   WHERE Row_ID < 4
   ORDER BY recipient) AS t
GROUP BY recipient
HAVING sum(amount) >= 1024
ORDER BY recipient ASC;


SELECT t.recipient
FROM (
       SELECT
         recipient,
         sum(amount)            AS total_amount,
         count(DISTINCT sender) AS d_sender_count,
         count(sender)          AS sender_count
       FROM transfers
       GROUP BY recipient) AS t
WHERE t.total_amount > 1024 AND (sender_count < 3 OR (sender_count >= 3 AND d_sender_count >= 3))
ORDER BY t.recipient ASC;


SELECT t.recipient
FROM (
       SELECT
         recipient,
         nth_value(amount, 3)
         OVER (
           ORDER BY amount DESC ) AS top_3_amount
       FROM transfers
       GROUP BY recipient
       ORDER BY amount) AS t
WHERE t.top_3_amount > 1024
ORDER BY t.recipient ASC;

nth_value(amount, 3)
IGNORE NULLS
OVER ( PARTITION BY recipient ORDER BY amount DESC
ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING )
AS top_3_amount


SELECT DISTINCT t1.recipient
FROM transfers t1
WHERE 1024 >= (SELECT sum(amount)
               FROM transfers
               WHERE recipient = t1.recipient
               ORDER BY amount
               LIMIT 3)
ORDER BY t1.recipient ASC;

company  val
---------------
com1     5
com1     4
com1     3
com2     55
com2     44
com2     33
com3     555
com3     444
com3     333

SELECT
  company,
  val
FROM
  (
    SELECT
      *,
      ROW_NUMBER()
      OVER (
        PARTITION BY
          company
        ORDER BY val DESC ) AS Row_ID
    FROM com
  ) AS A
WHERE Row_ID < 4
ORDER BY company

