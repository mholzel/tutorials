function cost = fmod(xyz)
x    = reshape( xyz( 0 + (1:22)), 2, 11 );
y    = reshape( xyz(22 + (1:12)), 3, 4  );
z    = reshape( xyz(34 + (1:35)), 5, 7  );
cost = norm(x - 5) + norm(y - 10) + norm(z - 1);