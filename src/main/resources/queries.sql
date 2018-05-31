SELECT
  count(*)                                                             AS requests,
  ip,
  current_timestamp                                                    AS blockedDate,
  'blocked because it exceeded the threshold of 500 requests in 1 DAY' AS comment
FROM USER_LOG
WHERE date BETWEEN '2017-01-01.00:00:00' AND adddate(date_format('2017-01-01.00:00:00', '%Y-%m-%d.%H:%i:%s'),
                                                     INTERVAL 1 DAY)
GROUP BY ip
HAVING requests >= 500;


SELECT count(*)
FROM USER_LOG
WHERE ip = '192.168.129.191';