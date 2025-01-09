LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
ENTITY add_sub IS
  PORT (
    a, b : IN STD_ULOGIC_VECTOR(7 DOWNTO 0);
    sub : IN STD_ULOGIC;
    result : OUT STD_ULOGIC_VECTOR(7 DOWNTO 0));
END;

LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
ENTITY adder IS
  PORT (
    a, b : IN STD_ULOGIC_VECTOR(7 DOWNTO 0);
    cin : IN STD_ULOGIC;
    cout : OUT STD_ULOGIC;
    sum : OUT STD_ULOGIC_VECTOR(7 DOWNTO 0));
END;

LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
ENTITY FA IS
  PORT (
    a, b, cin : IN STD_ULOGIC;
    cout, s : OUT STD_ULOGIC);
END;

LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
ENTITY HA IS
  PORT (
    a, b : IN STD_ULOGIC;
    c : OUT STD_ULOGIC;
    s : OUT STD_ULOGIC);
END HA;

ARCHITECTURE structural OF add_sub IS
  COMPONENT adder
    PORT (
      a, b : IN STD_ULOGIC_VECTOR(7 DOWNTO 0);
      cin : IN STD_ULOGIC;
      cout : OUT STD_ULOGIC;
      sum : OUT STD_ULOGIC_VECTOR(7 DOWNTO 0));
  END COMPONENT;
  SIGNAL bx : STD_ULOGIC_VECTOR(7 DOWNTO 0);
BEGIN
  bx <= b XOR (7 downto 0 => sub);

  a0 : adder PORT MAP(a, bx, sub, OPEN, result);
END structural;

ARCHITECTURE structural OF adder IS
  COMPONENT FA
    PORT (
      a, b, cin : IN STD_ULOGIC;
      cout, s : OUT STD_ULOGIC);
  END COMPONENT;
  SIGNAL c1, c2, c3, c4, c5, c6, c7 : STD_ULOGIC;
BEGIN
  fa0 : FA PORT MAP(a(0), b(0), cin, c1, sum(0));
  fa1 : FA PORT MAP(a(1), b(1), c1, c2, sum(1));
  fa2 : FA PORT MAP(a(2), b(2), c2, c3, sum(2));
  fa3 : FA PORT MAP(a(3), b(3), c3, c4, sum(3));
  fa4 : FA PORT MAP(a(4), b(4), c4, c5, sum(4));
  fa5 : FA PORT MAP(a(5), b(5), c5, c6, sum(5));
  fa6 : FA PORT MAP(a(6), b(6), c6, c7, sum(6));
  fa7 : FA PORT MAP(a(7), b(7), c7, cout, sum(7));
END;

ARCHITECTURE structural OF FA IS
  COMPONENT HA
    PORT (
      a, b : IN STD_ULOGIC;
      c : OUT STD_ULOGIC;
      s : OUT STD_ULOGIC);
  END COMPONENT;
  SIGNAL x, y, z : STD_ULOGIC;
BEGIN
  ha0 : HA PORT MAP(a, b, x, y);
  ha1 : HA PORT MAP(y, cin, z, s);
  cout <= x OR z;
END structural;

ARCHITECTURE structural OF HA IS
BEGIN
  c <= a AND b;
  s <= a XOR b;
END structural;