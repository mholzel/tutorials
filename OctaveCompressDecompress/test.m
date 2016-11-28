clear;clc;close all

x0   = zeros(2,11);
x    = fminunc( @g, x0 )

x0   = zeros(2,11);
y0   = zeros(3,4);
z0   = zeros(5,7);
xyz0 = [ x0(:) ; y0(:) ; z0(:) ];
xyz  = fminunc( @fmod, xyz0 )

x0   = zeros(2,11);
y0   = zeros(3,4);
z0   = zeros(5,7);
xyz0 = press( 'xyzKey', x0, y0, z0 );
xyz  = fminunc( @fnew, xyz0 )