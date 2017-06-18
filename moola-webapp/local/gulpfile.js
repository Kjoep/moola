'use strict';

var browserSync = require('browser-sync').create();

// grab our gulp packages
var gulp  = require('gulp'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    inject = require('gulp-inject'),
    clean = require('gulp-clean'),
    debug = require('gulp-debug');

var SRC_FOLDER = '../src/main/webapp/';
var TARGET_FOLDER = 'dist/';

var src = function(sources){
    return gulp.src(inSrc(sources), {base: SRC_FOLDER});
}

var inSrc = function(files){
    var map = function(entry){
        if (entry[0]=='!')
            return !+SRC_FOLDER+entry.substring(1);
        else
            return SRC_FOLDER+entry;
    };
    if (files instanceof Array)
        return files.map(map);
    else
        return map(files);
}

gulp.task('watch', ['build'], function(){

    browserSync.init({
        server: [TARGET_FOLDER]
    });

    gulp.watch(inSrc('**/*.js'), ['bundle-js']);
    gulp.watch(inSrc('**/*.less'), ['bundle-less', 'bundle-less-inject']);
    gulp.watch(inSrc('**/*.html'), ['move-html']);

    gulp.watch([TARGET_FOLDER + '**/*.html', TARGET_FOLDER + '**/*.js']).on('change', function(){
        browserSync.reload();
    });

});

function validate(){
    return SwaggerParser.validate("output/swagger.json", {validate: {spec: true}})
    .then(function(){
        fs.writeFileSync('output/errors.json', '{}');
        gutil.log('All valid.');
    })
    .catch(function(error){
        fs.writeFileSync('output/errors.json', JSON.stringify(error, null, 2));
        gutil.log(error);
    });
}

gulp.task('bundle-less', function(){
    return src('**/*.less')
        .pipe(less({paths: []}))
        .pipe(gulp.dest(TARGET_FOLDER));
});

gulp.task('bundle-less-inject', function(){
    return src('**/*.less')
        .pipe(less({paths: []}))
        .pipe(browserSync.stream());
});

gulp.task('copy-static', function(){
    return src(['lib/**/*', 'img/**/*'])
        .pipe(gulp.dest(TARGET_FOLDER));
})

gulp.task('move-html', ['bundle-less', 'bundle-js'], function(){
    var injectables = gulp.src([
        TARGET_FOLDER + 'lib/**/*.js',
        TARGET_FOLDER + 'moola.js',
        TARGET_FOLDER + '**/*.js',
        TARGET_FOLDER + '**/*.css'], {read: false})
    return src('**/*.html')
        .pipe(inject(injectables, {ignorePath: TARGET_FOLDER}))
        .pipe(gulp.dest(TARGET_FOLDER));
});

gulp.task('bundle-js', function(){
    return src('**/*.js', '!lib/**/*')
        .pipe(gulp.dest(TARGET_FOLDER));
})

gulp.task('clean', function(){
    return gulp.src(TARGET_FOLDER).pipe(clean());

})
gulp.task('build', ['bundle-less', 'move-html', 'bundle-js', 'copy-static']);
gulp.task('default', ['build']);
