-- 单元测试（H2）：与生产策略一致，users.id 自增从 100000000 起（由 Hibernate 建表后执行）
ALTER TABLE users ALTER COLUMN id RESTART WITH 100000000;
