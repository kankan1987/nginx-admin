/*******************************************************************************
 * Copyright 2016 JSL Solucoes LTDA - https://jslsolucoes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.jslsolucoes.nginx.admin.repository;

import java.io.IOException;
import java.util.List;

import com.jslsolucoes.nginx.admin.model.Upstream;
import com.jslsolucoes.nginx.admin.model.UpstreamServer;
import com.jslsolucoes.nginx.admin.repository.impl.OperationResult;
import com.jslsolucoes.nginx.admin.repository.impl.OperationType;

import freemarker.template.TemplateException;

public interface UpstreamRepository {

	public List<Upstream> listAll();

	public OperationType deleteWithResource(Upstream upstream) throws IOException;

	public Upstream load(Upstream upstream);

	public OperationResult saveOrUpdate(Upstream upstream, List<UpstreamServer> upstreamServers)
			throws IOException, TemplateException;

	public List<String> validateBeforeSaveOrUpdate(Upstream upstream, List<UpstreamServer> upstreamServers);

	public Upstream findByName(String name);

	public Upstream hasEquals(Upstream upstream);

}
