function varargout = press( key, varargin )

% This function is used to compress a list of input arguments to a single 
% vector, and then dempress those arguments into the proper shape. For 
% instance, typical usage is:
%
% % Compress some arguments into a single vector
% xyz = press( 'xyzKey', x, y, z );
% uv  = press(  'uvKey', u, v    );
%
% % Decompress the vector into the original arguments
% [ x, y, z ] = press( 'xyzKey', xyz )
% [ u, v    ] = press(  'uvKey', uv  )
%
% This function works by saving the sizes of the original arguments during 
% compression, and then using those saved sizes during decompression. Hence the 
% unique string that you associate with the compression/decompression is 
% important so that the code can differentiate which arguments you are using.  
%
% This function is particularly useful for functions such as optimization or 
% intergration functions that cannot handle a variable number of inputs. 
% For example, suppose we have an optimization function that can only optimize 
% functions of the form f(x), where x is a vector. Clearly such code can also 
% be used to optimize functions of the form f(x,y,z) with respect to (x,y,z), 
% but to do that, we have to do some tedious bookkeeping to modify the function 
% f so that we can use that optimization function. This code helps with that. 
% Specifically, this is how you would modify your code....
%
% First, you are going to compress your arguments so that they are in vector 
% form, since the optimization function requires that format. For instance, you 
% can compress some initial guesses (x0,y0,z0) into a single vector using:
%
% xyz0 = press( 'someUniqueString', x0, y0, z0 );
%
% where the string 'someUniqueString' should be a unique string that will be 
% used for compressing and decompressing the specific list of arguments (x,y,z).
%
% Next, you will modify your function definition from 
%
% function ... = f(x,y,z) 
%
% to
%
% function ... = f(xyz)
% [x,y,z] = press( 'someUniqueString', xyz );
%
% making sure that your 'someUniqueString' is the same as the one that you used 
% for compression. Basically though, that is all you have to do. Now all of your 
% code that worked only for a single vector input argument will work for any 
% number of arguments. 
%
% Notes: 
% - If you do not specify a key, then decompression will not work.
% - If you do not specify a key, then compression will work, but the sizes 
%   cannot be saved, so decompression of the same vector is not possible. 
% - If you specify a single input argument that is a vector, then we perform 
%   decompression. If you specify either multiple arguments, or a single 
%   argument that is not a vector, then we perform compression.
% - This function should be able to handle empty arguments and cell arrays.
%
% Gotchas: 
% - When you 'clear' your workspace, the saved sizes are lost. 
% - You must compress arguments before you can use decompression, otherwise the 
%   algorithm has no way of knowing what sizes the original arguments had. 

% If the internal size map has not been created, do it now. 
persistent sizes;
if isempty(sizes)
    sizes   = struct;
end

% If no arguments were specified, then we return the internal size map. 
if nargin == 0
    varargout{1}= sizes;
    return
elseif nargin == 1
    % If only one argument was specified, then we assume that you want to
    % compress or decompress that argument assuming an empty key. 
    varargin{1} = key;
    key         = '';
end

% Make sure that the key is a string
if ~ischar(key)
    error('The specified key is not a string.');
end

% If you gave one input argument, and it is a vector. Decompress it.
% Otherwise, compress the arguments.
if numel(varargin) == 1 && isvector(varargin{1})
    if ~isempty(sizes) && isfield(sizes,key)
        c               = 0;
        sz              = sizes.(key);
        n               = min(nargout,numel(sz));
        varargout       = cell( n, 1 );
        [varargout{:}]  = decompresser( sz(1:n) );
    else
        error('The decompression key was never used during compression.');
    end
else
    [ sz, n ]       = cellsize( varargin{:} );
    varargout{1}    = zeros(n,1);
    c               = 0;
    compresser( varargin{:} );
    
    % If the key isn't empty, save the sizes
    if ~isempty(key)
        sizes.(key) = sz;
    end
end

% Internal functions used for compression and decompression
    function compresser( varargin )
        for i = 1:nargin
            if iscell(varargin{i})
                for j = 1:numel(varargin{i})
                    compresser( varargin{i}{j} );
                end
            else
                nv                      = numel(varargin{i});
                varargout{1}(c+(1:nv))  = varargin{i}(:);
                c                       = c + nv;
            end
        end
    end

    function varargout = decompresser(sz)
        if iscell(sz)
            % There appears to be a bug in Octave 4.0.2 whereby you do not get 
            % the correct output if you replace 'outs' with 'varargout'. 
            % That probably has to do with recursion problems. In any case, the
            % following seems to work in Matlab and Octave
            outs    = cell( size(sz) );
            for i = 1:numel(sz)
                if iscell(sz{i})
                    outs{i}         = cell( size(sz{i}) );
                    [ outs{i}{:} ]	= decompresser( sz{i} );
                else
                    outs{i}         = decompresser( sz{i} );
                end
            end
            varargout = outs;
        else
            nv        = prod(sz);
            varargout = { reshape( varargin{1}(c+(1:nv)), sz ) };
            c         = c + nv;
        end
    end

end

function [ sz, n ] = cellsize( varargin )
% Sizes of the variable input arguments returned in a cell array, where
% the last entry is the total number of entries in varargin, which is
% useful for compression.
n   = 0;
sz  = work( varargin{:} );

    function sz = work( varargin )
        sz  = cell(size(varargin));
        for i = 1:nargin
            if iscell(varargin{i})
                sz{i}   = cell(size(varargin{i}));
                for j = 1:numel(varargin{i})
                    tsz         = work( varargin{i}{j} );
                    sz{i}{j}    = tsz{:};
                end
            else
                sz{i}   = size( varargin{i} );
                n       = n + numel( varargin{i} );
            end
        end
    end
end