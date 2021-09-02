branch_desc=$1;
branch_name="feature-${branch_desc}";

git checkout master
git pull
git checkout -b $branch_name
git push --set-upstream origin $branch_name