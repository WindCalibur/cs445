function [ fastaString, reference, Y, X, length, M, GX, GY] = backTrack( firstString, secondString, d, e, blosom, blosomLegend )
   [score, M, GY, GX] = A2Q2(firstString,secondString,d,e,blosom,blosomLegend);
   [score,currentIndex] = max(M(:));
   score
   [X, Y] = ind2sub(size(M),currentIndex);
   % because MatLab is horrible
   Y = Y - 1;
   X = X - 1;
   fastaString = firstString(X)
   reference = secondString(Y)
   length = 1;
   Y = Y - 1;
   X = X - 1;
   while (true)
       diag = M(X+1, Y+1);
       left = GY(X+1, Y+1);
       up = GX(X+1, Y+1);
       if ((diag == 0) && (max([diag,left,up]) == diag))
           break;
       end
       if ((diag >= up) && (diag >= left))
           fastaString = strcat(firstString(X), fastaString);
           reference = strcat(secondString(Y), reference);
           Y = Y-1;
           X = X-1;
       elseif ((up >= diag) && (up >= left))
           fastaString = strcat(firstString(X), fastaString);
           reference =  strcat('-',reference);
           Y = Y-1;
       elseif ((left >= diag) && (left >= up))
           fastaString = strcat('-', fastaString);
           reference =  strcat(secondString(Y), reference);
           X = X-1;
       end
       length = length + 1;
   end 
   Y = Y+1;
   X = X+1;
end