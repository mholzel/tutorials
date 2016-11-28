function cost = fnew(xyz)
[ x, y, z ] = press( 'xyzKey', xyz );
cost = norm(x - 5) + norm(y - 10) + norm(z - 1);