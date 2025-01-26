#include "GrayRender.h"

GrayRender::GrayRender() {

}

GrayRender::~GrayRender() {

}

void GrayRender::onSurfaceCreated() {
    EglHelper eglHelper = *new EglHelper();
    program = eglHelper.createProgram(VERTEX_SHADER, FRAG_SHADER_GREY);
    glBindAttribLocation(program, 0, "a_position");
    glBindAttribLocation(program, 1, "a_texCoord");
}

void GrayRender::onSurfaceChanged(int width, int height) {
    glViewport(0, 0, width, height);
}

void GrayRender::onDrawFrame(int textureId) {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glUniform1i(glGetUniformLocation(program, "sTexture"), 0);
    glUniformMatrix4fv(glGetUniformLocation(program, "uMVPMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glUniformMatrix4fv(glGetUniformLocation(program, "uSTMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glVertexAttribPointer(0, 2, GL_FLOAT, 0, 0, vertex);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 2, GL_FLOAT, 1, 0, texture);
    glEnableVertexAttribArray(1);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}