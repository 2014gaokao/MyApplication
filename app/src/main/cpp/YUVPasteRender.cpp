#include "YUVPasteRender.h"

YUVPasteRender::YUVPasteRender() {

}

YUVPasteRender::~YUVPasteRender() {

}

void YUVPasteRender::onSurfaceCreated() {
    EglHelper eglHelper = *new EglHelper();
    program = eglHelper.createProgram(VERTEX_SHADER, FRAG_SHADER_YUV);
    glBindAttribLocation(program, 0, "a_position");
    glBindAttribLocation(program, 1, "a_texCoord");
}

void YUVPasteRender::onSurfaceChanged(int width, int height) {
    glViewport(0, 0, width, height);
}

void YUVPasteRender::onDrawFrame(int textureId, int pasteTextureId) {
    glUseProgram(program);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
    glUniform1i(glGetUniformLocation(program, "sTexture"), 0);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, pasteTextureId);
    glUniform1i(glGetUniformLocation(program, "pTexture"), 1);

    glUniformMatrix4fv(glGetUniformLocation(program, "uMVPMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glUniformMatrix4fv(glGetUniformLocation(program, "uSTMatrix"), 1, GL_FALSE, IDENTITY_MATRIX);
    glVertexAttribPointer(0, 2, GL_FLOAT, 0, 0, vertex);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 2, GL_FLOAT, 1, 0, texture);
    glEnableVertexAttribArray(1);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}